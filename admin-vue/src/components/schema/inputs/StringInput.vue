<template>
    <v-text-field v-model="item[field.name]" box :label="field.name"></v-text-field>
</template>

<script>
  export default {
    name: "string-input",
    props: {field: Object, item: Object}
  };
</script>

