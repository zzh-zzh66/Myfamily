<template>
  <div
    class="member-node"
    :class="[`member-node--${node.gender}`, { 'member-node--selected': isSelected }]"
    :style="nodeStyle"
    @click="handleClick"
    @dblclick="handleDbClick"
  >
    <div class="node-avatar">
      <img v-if="node.avatar" :src="node.avatar" :alt="node.name" />
      <span v-else class="avatar-placeholder">{{ node.name?.charAt(0) }}</span>
    </div>
    <div class="node-info">
      <div class="node-name">{{ node.name }}</div>
      <div class="node-generation">第{{ node.generation }}代</div>
    </div>
    <div v-if="node.spouse" class="node-spouse">
      <span class="spouse-name">{{ node.spouse }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { GenealogyNode } from '@/types/api'

const props = defineProps<{
  node: GenealogyNode
  isSelected?: boolean
}>()

const emit = defineEmits<{
  (e: 'click', node: GenealogyNode): void
  (e: 'dblclick', node: GenealogyNode): void
}>()

const nodeStyle = computed(() => ({
  transform: `scale(${props.node.isHighlighted ? 1.1 : 1})`
}))

function handleClick() {
  emit('click', props.node)
}

function handleDbClick() {
  emit('dblclick', props.node)
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.member-node {
  @include node-base;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 100px;
  cursor: pointer;

  &--male {
    @include node-male;
  }

  &--female {
    @include node-female;
  }

  &--selected {
    box-shadow: 0 0 0 3px $color-primary, 0 8px 24px rgba($color-primary, 0.4);
    transform: scale(1.05);
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  }
}

.node-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: $spacing-xs;
  border: 2px solid rgba(255, 255, 255, 0.3);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .avatar-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    background: rgba(255, 255, 255, 0.2);
    font-size: 20px;
  }
}

.node-info {
  text-align: center;

  .node-name {
    font-size: $font-size-sm;
    font-weight: 600;
    margin-bottom: 2px;
  }

  .node-generation {
    font-size: $font-size-xs;
    opacity: 0.8;
  }
}

.node-spouse {
  margin-top: $spacing-xs;
  padding-top: $spacing-xs;
  border-top: 1px dashed rgba(255, 255, 255, 0.3);
  width: 80%;

  .spouse-name {
    font-size: $font-size-xs;
    opacity: 0.9;
  }
}
</style>
