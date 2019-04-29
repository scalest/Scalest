<template>
    <span>{{item[field.name]}}</span>
</template>

<script>
  export default {
    name: "int-output",
    props: {field: Object, item: Object}
  };
</script>

