<template>
  <div class="my-card" :class="{ 'my-card--clickable': clickable }">
    <div v-if="$slots.header" class="my-card__header">
      <slot name="header" />
    </div>
    <div class="my-card__body">
      <slot />
    </div>
    <div v-if="$slots.footer" class="my-card__footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  clickable?: boolean
}>()
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.my-card {
  @include card;

  &--clickable {
    cursor: pointer;
    transition: transform $transition-fast ease, box-shadow $transition-fast ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: $shadow-lg;
    }
  }

  &__header {
    padding-bottom: $spacing-md;
    margin-bottom: $spacing-md;
    border-bottom: 1px solid $color-border-light;
  }

  &__body {
    // 默认内容区域
  }

  &__footer {
    padding-top: $spacing-md;
    margin-top: $spacing-md;
    border-top: 1px solid $color-border-light;
  }
}
</style>
