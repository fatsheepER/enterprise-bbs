<script setup>
import { computed, onMounted, reactive, ref, watch, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import {
  createAdminBoard,
  getAdminBoards,
  updateAdminBoard,
} from '@/api/admin'
import AdminTable from '@/components/AdminTable.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const idQuery = ref('')
const keywordQuery = ref('')
const boards = ref([])
const isDialogOpen = ref(false)
const editingBoardId = ref(null)
const formError = ref('')

const form = reactive({
  name: '',
  description: '',
  colorHex: '#007aff',
  sortOrder: 1,
  status: 1,
})

const columns = [
  { key: 'id', label: 'ID', className: 'admin-boards-table__id', align: 'center' },
  { key: 'name', label: '名称', className: 'admin-boards-table__name' },
  { key: 'colorHex', label: '颜色', className: 'admin-boards-table__color', align: 'center' },
  { key: 'sortOrder', label: '排序', className: 'admin-boards-table__sort', align: 'center' },
  { key: 'postCount', label: '帖子', className: 'admin-boards-table__count', align: 'center' },
  { key: 'replyCount', label: '回复', className: 'admin-boards-table__count', align: 'center' },
  { key: 'updatedAt', label: '更新时间', className: 'admin-boards-table__time' },
  { key: 'status', label: '状态', className: 'admin-boards-table__status', align: 'center' },
]

watchEffect(() => {
  if (!authStore.isLoggedIn) {
    router.replace({
      path: '/login',
      query: { redirect: route.fullPath },
    })

    return
  }

  if (!authStore.isAdmin) {
    router.replace('/')
  }
})

const normalizedIdQuery = computed(() => {
  const value = idQuery.value.trim()

  return value ? Number(value) : null
})

const boardFilters = computed(() => ({
  id: normalizedIdQuery.value,
  keyword: keywordQuery.value,
}))

async function loadBoards() {
  if (!authStore.isAdmin) {
    return
  }

  const { id, keyword } = boardFilters.value

  boards.value = await getAdminBoards({
    id,
    keyword,
  })
}

onMounted(loadBoards)
watch([idQuery, keywordQuery], loadBoards)

const activeBoards = computed(() => boards.value.filter((board) => board.status === 1))
const disabledBoards = computed(() => boards.value.filter((board) => board.status === 0))
const dialogTitle = computed(() => (editingBoardId.value ? '管理版块' : '新建版块'))
const submitText = computed(() => (editingBoardId.value ? '保存修改' : '创建版块'))

function nextSortOrder() {
  return Math.max(0, ...boards.value.map((board) => Number(board.sortOrder) || 0)) + 1
}

function resetForm() {
  form.name = ''
  form.description = ''
  form.colorHex = '#007aff'
  form.sortOrder = nextSortOrder()
  form.status = 1
  formError.value = ''
}

function openCreateDialog() {
  editingBoardId.value = null
  resetForm()
  isDialogOpen.value = true
}

function openEditDialog(board) {
  editingBoardId.value = board.id
  form.name = board.name
  form.description = board.description
  form.colorHex = board.colorHex
  form.sortOrder = board.sortOrder
  form.status = board.status
  formError.value = ''
  isDialogOpen.value = true
}

function closeDialog() {
  isDialogOpen.value = false
  formError.value = ''
}

async function saveBoard() {
  try {
    const payload = {
      name: form.name,
      description: form.description,
      colorHex: form.colorHex,
      sortOrder: form.sortOrder,
      status: form.status,
    }

    if (editingBoardId.value) {
      await updateAdminBoard(editingBoardId.value, payload)
    } else {
      await createAdminBoard(payload)
    }

    await loadBoards()
    closeDialog()
  } catch (error) {
    formError.value = error.message
  }
}

function statusText(status) {
  return status === 1 ? '启用' : '停用'
}

function splitDateTime(dateTime) {
  const [date, time = ''] = dateTime.split(' ')

  return {
    date,
    time,
  }
}
</script>

<template>
  <section v-if="authStore.isAdmin" class="admin-boards-page">
    <header class="admin-boards-page__header">
      <div>
        <RouterLink class="admin-boards-page__back" to="/admin">管理员控制台</RouterLink>
        <h1 class="admin-boards-page__title">版块管理</h1>
      </div>
      <button class="admin-boards-page__create" type="button" @click="openCreateDialog">
        新建版块
      </button>
    </header>

    <form class="admin-boards-filter" @submit.prevent>
      <label class="admin-boards-filter__field">
        <span>ID</span>
        <input v-model="idQuery" type="number" min="1" placeholder="精确搜索" />
      </label>

      <label class="admin-boards-filter__field admin-boards-filter__field--keyword">
        <span>名称</span>
        <input v-model="keywordQuery" type="search" placeholder="按版块名称搜索" />
      </label>
    </form>

    <section class="admin-boards-section" aria-labelledby="active-boards-title">
      <div class="admin-boards-section__header">
        <h2 id="active-boards-title" class="admin-boards-section__title">启用版块</h2>
        <span class="admin-boards-section__count">{{ activeBoards.length }}</span>
      </div>

      <AdminTable
        :columns="columns"
        :rows="activeBoards"
        aria-label="启用版块列表"
        empty-text="暂无启用版块"
      >
        <template #cell-name="{ row, value }">
          <RouterLink
            class="admin-boards-table__name-link"
            :to="{ name: 'board-detail', params: { id: row.id } }"
          >
            {{ value }}
          </RouterLink>
        </template>

        <template #cell-colorHex="{ value }">
          <span
            class="admin-boards-color__swatch"
            :style="{ backgroundColor: value }"
            :title="value"
          />
        </template>

        <template #cell-updatedAt="{ value }">
          <time class="admin-boards-time">
            <span>{{ splitDateTime(value).date }}</span>
            <span>{{ splitDateTime(value).time }}</span>
          </time>
        </template>

        <template #cell-status="{ value }">
          <span class="admin-table__status">{{ statusText(value) }}</span>
        </template>

        <template #actions="{ row }">
          <button class="admin-table__action" type="button" @click="openEditDialog(row)">
            管理
          </button>
        </template>
      </AdminTable>
    </section>

    <section class="admin-boards-section" aria-labelledby="disabled-boards-title">
      <div class="admin-boards-section__header">
        <h2 id="disabled-boards-title" class="admin-boards-section__title">停用版块</h2>
        <span class="admin-boards-section__count admin-boards-section__count--disabled">
          {{ disabledBoards.length }}
        </span>
      </div>

      <AdminTable
        :columns="columns"
        :rows="disabledBoards"
        aria-label="停用版块列表"
        empty-text="暂无停用版块"
      >
        <template #cell-colorHex="{ value }">
          <span
            class="admin-boards-color__swatch"
            :style="{ backgroundColor: value }"
            :title="value"
          />
        </template>

        <template #cell-updatedAt="{ value }">
          <time class="admin-boards-time">
            <span>{{ splitDateTime(value).date }}</span>
            <span>{{ splitDateTime(value).time }}</span>
          </time>
        </template>

        <template #cell-status="{ value }">
          <span class="admin-table__status admin-table__status--disabled">
            {{ statusText(value) }}
          </span>
        </template>

        <template #actions="{ row }">
          <button class="admin-table__action" type="button" @click="openEditDialog(row)">
            管理
          </button>
        </template>
      </AdminTable>
    </section>

    <div v-if="isDialogOpen" class="admin-board-dialog" role="dialog" aria-modal="true">
      <form class="admin-board-dialog__panel" @submit.prevent="saveBoard">
        <div class="admin-board-dialog__header">
          <h2 class="admin-board-dialog__title">{{ dialogTitle }}</h2>
          <button class="admin-board-dialog__close" type="button" aria-label="关闭" @click="closeDialog">
            ×
          </button>
        </div>

        <label class="admin-board-form__field">
          <span>版块名称</span>
          <input v-model="form.name" maxlength="50" required />
        </label>

        <label class="admin-board-form__field">
          <span>版块描述</span>
          <textarea v-model="form.description" rows="4" />
        </label>

        <div class="admin-board-form__grid">
          <label class="admin-board-form__field">
            <span>展示颜色</span>
            <input v-model="form.colorHex" type="color" />
          </label>

          <label class="admin-board-form__field">
            <span>排序值</span>
            <input v-model.number="form.sortOrder" type="number" min="0" />
          </label>

          <label class="admin-board-form__field">
            <span>状态</span>
            <select v-model.number="form.status">
              <option :value="1">启用</option>
              <option :value="0">停用</option>
            </select>
          </label>
        </div>

        <p v-if="formError" class="admin-board-form__error">{{ formError }}</p>

        <div class="admin-board-dialog__actions">
          <button class="admin-board-dialog__cancel" type="button" @click="closeDialog">取消</button>
          <button class="admin-board-dialog__submit" type="submit">{{ submitText }}</button>
        </div>
      </form>
    </div>
  </section>
</template>
