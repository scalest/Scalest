<template>
    <span>{{item[field.name]}}</span>
</template>

<script>
  export default {
    name: "list-output",
    props: {field: Object, item: Object}
  };
</script>

