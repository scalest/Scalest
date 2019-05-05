<template>
    <v-select
            v-model="item[field.name]"
            :label="field.name"
            box
            :items="field.info.addition.values"></v-select>
</template>

<script>
  export default {
    name: "enum-input",
    props: {field: Object, item: Object}
  };
</script>

