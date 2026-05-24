<script setup>
defineProps({
  columns: {
    type: Array,
    required: true,
  },
  rows: {
    type: Array,
    required: true,
  },
  rowKey: {
    type: String,
    default: 'id',
  },
  emptyText: {
    type: String,
    default: '暂无数据',
  },
  ariaLabel: {
    type: String,
    default: '管理员表格',
  },
})

function cellClass(column) {
  return [
    'admin-table__cell',
    column.className,
    column.align === 'center' ? 'admin-table__cell--center' : '',
    column.align === 'right' ? 'admin-table__cell--right' : '',
  ]
}

function rowIdentity(row, rowKey) {
  return row[rowKey]
}
</script>

<template>
  <section class="admin-table-wrap" :aria-label="ariaLabel">
    <table class="admin-table">
      <thead>
        <tr>
          <th
            v-for="column in columns"
            :key="column.key"
            :class="cellClass(column)"
            scope="col"
          >
            {{ column.label }}
          </th>
          <th v-if="$slots.actions" class="admin-table__cell admin-table__cell--actions" scope="col">
            操作
          </th>
        </tr>
      </thead>

      <tbody>
        <tr v-if="rows.length === 0">
          <td class="admin-table__empty" :colspan="columns.length + ($slots.actions ? 1 : 0)">
            {{ emptyText }}
          </td>
        </tr>

        <template v-else>
          <tr v-for="row in rows" :key="rowIdentity(row, rowKey)" class="admin-table__row">
            <td v-for="column in columns" :key="column.key" :class="cellClass(column)">
              <slot :name="`cell-${column.key}`" :row="row" :value="row[column.key]">
                {{ row[column.key] }}
              </slot>
            </td>

            <td v-if="$slots.actions" class="admin-table__cell admin-table__cell--actions">
              <slot name="actions" :row="row" />
            </td>
          </tr>
        </template>
      </tbody>
    </table>
  </section>
</template>
