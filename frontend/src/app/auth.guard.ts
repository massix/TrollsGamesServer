import { Injectable } from '@angular/core';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot, CanActivate } from '@angular/router';
import { AlertService } from './services/alert.service';
import { CrawlService } from './services/crawl.service';
import 'rxjs/add/operator/toPromise';

@Injectable()
export class AuthGuard implements CanActivate {
    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
        return new Promise((resolve) => {
            if (localStorage.getItem('token')) {
                this.crawlService.getStatus().subscribe(
                    ok => {
                        console.log('Auth guard resolving to OK');
                        resolve(true);
                    },
                    ko => {
                        console.log('Auth guard resolving to KO');
                        localStorage.removeItem('token');
                        this.router.navigate(['/login']);
                        this.alertService.error('You are not logged in!', true);
                        resolve(false);
                });
            } else {
                console.log('User not logged in');
                this.router.navigate(['/login']);
                this.alertService.error('You are not logged in!', true);
                resolve(false);
            }
        });
    }

    constructor(private router: Router, private alertService: AlertService, private crawlService: CrawlService) {}
}
