"""秒杀并发压测 — 自动清理 + 多场景执行"""
import concurrent.futures, requests, time, subprocess, sys

URL = 'http://localhost:8800/portal/seckill/execute?sessionId=99'
CSV = 'target/benchmark-tokens.csv'
tokens = []
with open(CSV) as f:
    next(f)
    for line in f:
        tokens.append(line.strip().split(',')[0])

def seckill(token):
    try:
        r = requests.post(URL, headers={'mall-token': token}, timeout=30)
        return r.status_code, r.json().get('code'), r.elapsed.total_seconds()
    except:
        return 0, -1, 0

scenarios = [
    ('S2-200并发/500库存', 200, 500),
    ('S3-500并发/500库存', 500, 500),
    ('S4-1000超额(1:2)', 1000, 500),
    ('S5-1000极限(1:5)', 1000, 200),
]

offset = 0
results = []

for name, users, stock in scenarios:
    print(f'\n{"="*60}')
    print(f'  {name}')
    print(f'{"="*60}')

    # 重启后端
    print('[1/4] Restarting backend...')
    # Kill backend by port (Windows cmd syntax)
    subprocess.run('cmd /c "for /f \\"tokens=5\\" %a in (\'netstat -ano ^| findstr :8800.*LISTENING\') do taskkill //F //PID %a"',
                   shell=True, capture_output=True)
    time.sleep(3)
    # Purge queues
    for q in ['seckill.order.queue','seckill.retry.delay.queue','order.delay.queue','order.timeout.queue']:
        subprocess.run(f'docker exec mall-rabbitmq rabbitmqctl purge_queue {q}', shell=True, capture_output=True)

    # Clean DB + Redis
    subprocess.run('docker exec mall-mysql mysql -uroot -p123456 mall -e '
                   '"DELETE FROM oms_order_item WHERE order_id IN (SELECT id FROM oms_order WHERE seckill_session_id=99);'
                   'DELETE FROM oms_order WHERE seckill_session_id=99;'
                   f'UPDATE pms_product SET stock={stock+100} WHERE id=19;"', shell=True, capture_output=True)
    # Clear all seckill session 99 keys from Redis DB 1
    clear_cmd = 'docker exec mall-redis redis-cli -a 123456 --no-auth-warning -n 1 KEYS "seckill:*99*"'
    r = subprocess.run(clear_cmd, shell=True, capture_output=True, text=True)
    for key in r.stdout.strip().split('\n'):
        if key:
            subprocess.run(f'docker exec mall-redis redis-cli -a 123456 --no-auth-warning -n 1 DEL {key}',
                          shell=True, capture_output=True)
    subprocess.run(f'docker exec mall-redis redis-cli -a 123456 --no-auth-warning -n 1 '
                   f'SET seckill:stock:99 {stock}', shell=True, capture_output=True)

    # Start backend
    subprocess.Popen(['cmd', '/c', 'mvnw.cmd', 'spring-boot:run', '-Dspring-boot.run.profiles=dev'],
                     cwd='D:/project/idea/mall/mall-server', stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

    # Wait for backend
    print('[2/4] Waiting for backend...', end='', flush=True)
    for i in range(60):
        try:
            r = requests.get('http://localhost:8800/actuator/health', timeout=2)
            if r.status_code == 200:
                print(' OK')
                break
        except:
            pass
        print('.', end='', flush=True)
        time.sleep(2)

    # Run test
    print(f'[3/4] Testing: {users} users, token range [{offset}, {offset+users})')
    start = time.time()
    futures_results = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=min(users, 200)) as ex:
        futures = [ex.submit(seckill, tokens[offset + i]) for i in range(users)]
        for f in concurrent.futures.as_completed(futures):
            futures_results.append(f.result())
    elapsed = time.time() - start

    success = sum(1 for r in futures_results if r[1] == 200)
    dupes = sum(1 for r in futures_results if r[1] == 1803)
    stock_empty = sum(1 for r in futures_results if r[1] == 1802)
    errors = sum(1 for r in futures_results if r[1] not in (200, 1802, 1803))
    times = [r[2]*1000 for r in futures_results if r[2] > 0]
    avg_t = sum(times)/len(times) if times else 0

    # Wait for MQ to drain
    print('[4/4] Waiting for MQ...', end='', flush=True)
    time.sleep(5)
    for i in range(30):
        r = subprocess.run('docker exec mall-rabbitmq rabbitmqctl list_queues name messages',
                          shell=True, capture_output=True, text=True)
        total = sum(int(l.split()[1]) for l in r.stdout.strip().split('\n')[1:] if l.split()[1].isdigit())
        if total == 0:
            print(' OK')
            break
        time.sleep(2)

    # Get final stock and orders
    r = subprocess.run('docker exec mall-redis redis-cli -a 123456 --no-auth-warning -n 1 GET seckill:stock:99',
                      shell=True, capture_output=True, text=True)
    remain_stock = int(r.stdout.strip().strip('"')) if r.stdout.strip() else 0
    r = subprocess.run('docker exec mall-mysql mysql -uroot -p123456 mall -N -e "SELECT COUNT(*) FROM oms_order WHERE seckill_session_id=99"',
                      shell=True, capture_output=True, text=True)
    order_count = int(r.stdout.strip()) if r.stdout.strip() else 0

    deducted = stock - remain_stock
    status = '✅' if (deducted == order_count and errors == 0) else '⚠️'

    print(f'\n{status} {name}')
    print(f'   Elapsed: {elapsed:.1f}s | Throughput: {users/elapsed:.0f} req/s')
    print(f'   Success: {success} | StockEmpty: {stock_empty} | Dup: {dupes} | Err: {errors}')
    print(f'   Response: min={min(times):.0f}ms max={max(times):.0f}ms avg={avg_t:.0f}ms')
    print(f'   Stock: {stock}->{remain_stock} (deducted {deducted}) | Orders: {order_count}')
    print(f'   Match: deducted({deducted}) == orders({order_count}) -> {deducted == order_count}')

    results.append((name, success, deducted, order_count, deducted == order_count, elapsed, avg_t))
    offset += users

print(f'\n{"="*60}')
print('  FINAL SUMMARY')
print(f'{"="*60}')
for name, succ, ded, ords, match, elapsed, avg_t in results:
    s = '✅' if match else '❌'
    print(f'{s} {name}: OK={succ} deducted={ded} orders={ords} match={match} {elapsed:.1f}s avg={avg_t:.0f}ms')
