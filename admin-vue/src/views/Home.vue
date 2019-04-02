<template>
  <div class="home">
    <scalest-header/>
    <v-layout class="align-center justify-center row">
      <v-carousel>
        <v-carousel-item class="my-1" key="0">
          <v-container class="fill-heigth">
            <v-layout class="align-center justify-center">
              <img
                :src="`${publicPath}/static/images/scalest.png`"
                alt="scalest-logo"
                style="max-width:100%;max-height:100%;"
              >
            </v-layout>
          </v-container>
        </v-carousel-item>
      </v-carousel>
    </v-layout>
  </div>
</template>

<script>
import ScalestHeader from "../components/ScalestHeader.vue";

export default {
  name: "home",
  components: {
    ScalestHeader
  },
  data() {
    return {
      publicPath: process.env.VUE_APP_BACKEND_SERVICE_URL
    };
  }
};
</script>
