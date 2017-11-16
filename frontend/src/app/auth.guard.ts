import { Injectable } from '@angular/core';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot, CanActivate } from '@angular/router';
import { AlertService } from './alert.service';

@Injectable()
export class AuthGuard implements CanActivate {
    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        console.log('can activate?');
        if (localStorage.getItem('token')) {
            return true;
        }

        this.router.navigate(['/login']);
        this.alertService.error('You are not logged in', true);
    }

    constructor(private router: Router, private alertService: AlertService) {}

}