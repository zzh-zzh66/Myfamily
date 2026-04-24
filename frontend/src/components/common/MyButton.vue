<template>
  <button
    class="my-button"
    :class="[
      `my-button--${type}`,
      `my-button--${size}`,
      { 'my-button--disabled': disabled, 'my-button--loading': loading }
    ]"
    :disabled="disabled || loading"
    @click="$emit('click', $event)"
  >
    <span v-if="loading" class="my-button__loading"></span>
    <slot />
  </button>
</template>

<script setup lang="ts">
defineProps<{
  type?: 'primary' | 'secondary' | 'text' | 'danger'
  size?: 'small' | 'medium' | 'large'
  disabled?: boolean
  loading?: boolean
}>()

defineEmits<{
  (e: 'click', event: MouseEvent): void
}>()
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.my-button {
  @include button-base;

  &--primary {
    @include button-primary;
  }

  &--secondary {
    @include button-secondary;
  }

  &--text {
    background: transparent;
    color: $color-primary;
    padding: 8px 16px;

    &:hover:not(:disabled) {
      background: rgba($color-primary, 0.05);
    }
  }

  &--danger {
    background: $color-danger;
    color: white;

    &:hover:not(:disabled) {
      background: darken($color-danger, 10%);
    }
  }

  &--small {
    padding: 8px 16px;
    font-size: $font-size-sm;
  }

  &--medium {
    padding: 12px 24px;
    font-size: $font-size-base;
  }

  &--large {
    padding: 16px 32px;
    font-size: $font-size-lg;
  }

  &--disabled,
  &--loading {
    opacity: 0.6;
    cursor: not-allowed;
  }

  &__loading {
    display: inline-block;
    width: 14px;
    height: 14px;
    margin-right: 8px;
    border: 2px solid currentColor;
    border-top-color: transparent;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
