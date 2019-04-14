 
<template>
  <v-chip class="primary" text-color="white">{{item[field.name]}}</v-chip>
</template>

<script>
export default {
  name: "enum-output",
  props: { field: Object, item: Object }
};
</script>

