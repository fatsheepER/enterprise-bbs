<script setup>
const options = [
  { value: 'latest', label: '最近回复' },
  { value: 'views', label: '浏览数' },
  { value: 'replies', label: '回复数' },
  { value: 'title', label: '标题首字母' },
]

defineProps({
  modelValue: {
    type: String,
    default: 'latest',
  },
})

const emit = defineEmits(['update:modelValue'])

function updateSort(event) {
  emit('update:modelValue', event.target.value)
}
</script>

<template>
  <label class="post-sort-select">
    <span class="post-sort-select__label">排序</span>
    <select
      class="post-sort-select__control"
      :value="modelValue"
      aria-label="帖子排序方式"
      @change="updateSort"
    >
      <option v-for="option in options" :key="option.value" :value="option.value">
        {{ option.label }}
      </option>
    </select>
  </label>
</template>
