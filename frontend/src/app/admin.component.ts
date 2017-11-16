import { Component, OnInit } from '@angular/core';
import { User } from './user';
import { LoginService } from './login.service';
import { AlertService } from './alert.service';
import { Router } from '@angular/router';

declare var jquery: any;
declare var $: any;


@Component({
    moduleId: module.id,
    templateUrl: 'admin.component.html',
    styleUrls: ['admin.component.css']
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
