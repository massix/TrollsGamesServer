import { Injectable } from '@angular/core';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot, CanActivate } from '@angular/router';
import { AlertService } from './services/alert.service';
import { CrawlService } from './services/crawl.service';
import 'rxjs/add/operator/toPromise';

@Injectable()
export class AuthGuard implements CanActivate {
    async canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        if (localStorage.getItem('token')) {
            try {
                const resp = await this.crawlService.getStatus().toPromise();
            } catch (e) {
                localStorage.removeItem('token');
                this.router.navigate(['/login']);
                this.alertService.error('You are not logged in!', true);
            }
            return true;
        }

        this.router.navigate(['/login']);
        this.alertService.error('You are not logged in', true);
    }

    constructor(private router: Router, private alertService: AlertService, private crawlService: CrawlService) {}

}