 
<template>
  <v-icon :color="color">{{icon}}</v-icon>
</template>

<script>
export default {
  name: "bool-output",
  props: { field: Object, item: Object },
  computed: {
    color: function() {
      return this.item[this.field.name] ? "green" : "red";
    },
    icon: function() {
      return this.item[this.field.name] ? "check_circle" : "cancel";
    }
  }
};
</script>

