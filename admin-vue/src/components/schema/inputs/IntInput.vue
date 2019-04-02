<template>
  <v-text-field v-model.number="item[field.name]" box :label="field.name" type="number"/>
</template>

<script>
export default {
  name: "int-input",
  props: { field: Object, item: Object }
};
</script>

