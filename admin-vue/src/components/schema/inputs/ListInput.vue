<template>
    <div>{{item[field.name]}}</div>
</template>

<script>
  export default {
    name: "list-input",
    props: {field: Object, item: Object}
  };
</script>

