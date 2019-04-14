 
<template>
  <v-switch v-model="item[field.name]" box :label="field.name"/>
</template>

<script>
export default {
  name: "bool-input",
  props: { field: Object, item: Object }
};
</script>

