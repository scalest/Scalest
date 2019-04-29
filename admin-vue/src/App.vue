<template>
    <v-app>
        <v-content>
            <v-container>
                <v-snackbar v-model="notification">{{notificationText}}</v-snackbar>
                <router-view @notification="addNotification"></router-view>
            </v-container>
        </v-content>
    </v-app>
</template>

<script>
  export default {
    data: function () {
      return {
        notificationText: "",
        notificationQueue: [],
        notification: false
      };
    },
    computed: {
      hasNotificationsPending() {
        return this.notificationQueue.length > 0;
      }
    },
    watch: {
      notification() {
        if (!this.notification && this.hasNotificationsPending) {
          this.notificationText = this.notificationQueue.shift();
          this.$nextTick(() => (this.notification = true));
        }
      }
    },
    methods: {
      addNotification(text) {
        this.notificationQueue.push(text);
        if (!this.notification) {
          this.notificationText = this.notificationQueue.shift();
          this.notification = true;
        }
      }
    }
  };
</script>

