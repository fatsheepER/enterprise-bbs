<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const options = [
  { value: 'latest', label: '最近回复' },
  { value: 'views', label: '浏览数' },
  { value: 'replies', label: '回复数' },
  { value: 'title', label: '标题首字母' },
]

const props = defineProps({
  modelValue: {
    type: String,
    default: 'latest',
  },
})

const emit = defineEmits(['update:modelValue'])
const menuRef = ref(null)
const isOpen = ref(false)
const selectedOption = computed(
  () => options.find((option) => option.value === props.modelValue) || options[0],
)

function toggleMenu() {
  isOpen.value = !isOpen.value
}

function selectOption(option) {
  isOpen.value = false
  emit('update:modelValue', option.value)
}

function handleDocumentPointerDown(event) {
  if (!menuRef.value?.contains(event.target)) {
    isOpen.value = false
  }
}

function handleEscape(event) {
  if (event.key === 'Escape') {
    isOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('pointerdown', handleDocumentPointerDown)
  document.addEventListener('keydown', handleEscape)
})

onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', handleDocumentPointerDown)
  document.removeEventListener('keydown', handleEscape)
})
</script>

<template>
  <div ref="menuRef" class="post-sort-menu">
    <button
      class="posts-sort-button"
      type="button"
      aria-haspopup="listbox"
      aria-label="选择帖子排序方式"
      :aria-expanded="isOpen"
      @click="toggleMenu"
    >
      {{ selectedOption.label }}
      <span aria-hidden="true">↓</span>
    </button>

    <div v-if="isOpen" class="post-sort-menu__options" role="listbox" aria-label="帖子排序方式">
      <button
        v-for="option in options"
        :key="option.value"
        class="post-sort-menu__option"
        :class="{ 'post-sort-menu__option--selected': option.value === selectedOption.value }"
        type="button"
        role="option"
        :aria-selected="option.value === selectedOption.value"
        @click="selectOption(option)"
      >
        {{ option.label }}
      </button>
    </div>
  </div>
</template>
