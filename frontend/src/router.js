
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import UserManager from "./components/UserManager"


import Dashboard from "./components/Dashboard"
import ReservationManager from "./components/ReservationManager"

import ReservationManager from "./components/ReservationManager"

import StockManager from "./components/StockManager"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/users',
                name: 'UserManager',
                component: UserManager
            },


            {
                path: '/dashboards',
                name: 'Dashboard',
                component: Dashboard
            },
            {
                path: '/reservations',
                name: 'ReservationManager',
                component: ReservationManager
            },

            {
                path: '/reservations',
                name: 'ReservationManager',
                component: ReservationManager
            },

            {
                path: '/stocks',
                name: 'StockManager',
                component: StockManager
            },



    ]
})
