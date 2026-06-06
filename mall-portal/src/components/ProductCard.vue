<template>
  <router-link :to="`/product/${product.id}`" class="product-card">
    <div class="card-img">
      <img v-if="imageUrl" :src="imageUrl" :alt="product.name" />
      <span v-else class="card-placeholder">{{ product.name?.charAt(0) }}</span>
      <div class="card-actions">
        <span class="quick-buy" @click.prevent="$emit('buy', product.id)">立即购买</span>
      </div>
    </div>
    <div class="card-body">
      <p class="card-name">{{ product.name }}</p>
      <p class="card-price"><em>&yen;</em>{{ product.price }}</p>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'
import { getImageUrl } from '@/api/product'

const props = defineProps({
  product: { type: Object, required: true },
  imageKey: { type: String, default: '' }
})

const imageUrl = computed(() => props.imageKey ? getImageUrl(props.imageKey) : '')
</script>

<style scoped>
.product-card { text-decoration: none; color: inherit; background: #fff; border-radius: 10px;
  overflow: hidden; transition: all .3s cubic-bezier(.4,0,.2,1); border: 1px solid #f0f0f0; }
.product-card:hover { transform: translateY(-4px); box-shadow: 0 12px 32px rgba(0,0,0,0.08); border-color: #ffcccc; }
.card-img { position: relative; aspect-ratio: 1; overflow: hidden; background: #fafafa; }
.card-img img { width: 100%; height: 100%; object-fit: cover; transition: transform .6s; }
.product-card:hover .card-img img { transform: scale(1.08); }
.card-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;
  font-size: 56px; color: #ddd; }
.card-actions { position: absolute; bottom: 0; left: 0; right: 0; padding: 12px;
  background: linear-gradient(transparent, rgba(0,0,0,0.3)); display: flex; justify-content: center;
  opacity: 0; transform: translateY(10px); transition: all .3s; }
.product-card:hover .card-actions { opacity: 1; transform: translateY(0); }
.quick-buy { padding: 6px 20px; background: #A10000; color: #fff; border-radius: 20px; font-size: 13px; }
.card-body { padding: 12px 14px 16px; }
.card-name { font-size: 14px; color: #333; margin: 0 0 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-price { margin: 0; font-size: 20px; font-weight: 700; color: #A10000; }
.card-price em { font-size: 14px; font-style: normal; margin-right: 2px; }
</style>
