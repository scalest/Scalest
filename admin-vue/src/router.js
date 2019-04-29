import Vue from 'vue'
import Router from 'vue-router'
import Home from './views/Home.vue'
import Login from './views/Login.vue'
import Model from './components/Model.vue'

Vue.use(Router);

const router = new Router({
  routes: [
    {path: '/', component: Home, meta: {auth: true}},
    {
      path: '/model/:name', component: Model, props: (route) => ({
        name: route.params.name
      }),
      meta: {auth: true}
    },
    {path: '/login', component: Login}
  ]
});

router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.auth)) {
    if (Vue.cookie.get('SCALEST_ADMIN') == null) {
      next({
        path: '/login',
        params: {nextUrl: to.fullPath}
      })
    } else {
      next()
    }
  } else {
    next()
  }
});

export default router
