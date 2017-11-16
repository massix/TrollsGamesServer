import { Component, OnInit } from '@angular/core';
import { User } from '../data/user';
import { LoginService } from '../services/login.service';
import { AlertService } from '../services/alert.service';
import { Router } from '@angular/router';

declare var jquery: any;
declare var $: any;


@Component({
    templateUrl: '../views/admin.component.html',
    styleUrls: ['../styles/admin.component.css']
})
export class AdminComponent implements OnInit {
    activeRoute = '';

    ngOnInit(): void {
    }

    setActiveRoute(activeRoute: string) {
        console.log('setting active route: ' + activeRoute);
        this.activeRoute = activeRoute;
    }

    constructor(private alert: AlertService, private router: Router) {}
}
