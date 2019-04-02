<template>
  <span>{{item[field.name]}}</span>
</template>

<script>
export default {
  name: "string-output",
  props: { field: Object, item: Object }
};
</script>

