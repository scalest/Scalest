<template>
  <v-app id="inspire">
    <v-content>
      <v-container fluid fill-height>
        <v-layout align-center justify-center>
          <v-flex xs12 sm8 md4>
            <v-card class="elevation-12">
              <v-toolbar dark color="primary">
                <v-toolbar-title>Login to Scalest Admin</v-toolbar-title>
              </v-toolbar>
              <v-card-text>
                <v-form>
                  <v-text-field
                    prepend-icon="person"
                    v-model="username"
                    name="login"
                    label="Login"
                    type="text"
                  ></v-text-field>
                  <v-text-field
                    id="password"
                    prepend-icon="lock"
                    v-model="password"
                    name="password"
                    label="Password"
                    type="password"
                  ></v-text-field>
                </v-form>
              </v-card-text>
              <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn @click="doLogin" color="primary">Login</v-btn>
              </v-card-actions>
            </v-card>
          </v-flex>
        </v-layout>
      </v-container>
    </v-content>
  </v-app>
</template>

<script>
import Axios from "axios";

export default {
  data: () => ({
    username: "",
    password: ""
  }),
  methods: {
    doLogin(e) {
      const loginRequest = { username: this.username, password: this.password };

      this.$http
        .post(
          `${process.env.VUE_APP_BACKEND_SERVICE_URL}/admin/login`,
          loginRequest
        )
        .then(r => {
          this.$cookie.set("SCALEST_ADMIN", r.data.token);

          Axios.defaults.headers.common["Authorization"] = `Basic ${
            r.data.token
          }`;

          const nextUrl = this.$route.params.nextUrl || "/";

          this.$router.push(nextUrl);
        })
        .catch(error => this.$emit("notification", "Can`t login"));
    }
  }
};
</script>