<template>
    <v-switch v-model="item[field.name]" box :label="field.name"></v-switch>
</template>

<script>
  export default {
    name: "bool-input",
    props: {field: Object, item: Object}
  };
</script>

