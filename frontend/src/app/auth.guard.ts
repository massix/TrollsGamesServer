import { CanActivate } from "@angular/router";
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot } from "@angular/router";
import { AlertService } from "./alert.service";
import { Injectable } from "@angular/core";

@Injectable()
export class AuthGuard implements CanActivate {
    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        if (localStorage.getItem('token')) {
            return true;
        }

        this.router.navigate(['/login']);
        this.alertService.error('You are not logged in', true);
    }

    constructor(private router: Router, private alertService: AlertService) {}

}