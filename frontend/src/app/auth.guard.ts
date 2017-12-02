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
                        resolve(true);
                    },
                    ko => {
                        localStorage.removeItem('token');
                        this.alertService.error('You are not logged in!', true);
                        resolve(false);
                        this.router.navigate(['/login']);
                    }
                );
            } else {
                resolve(false);
                this.router.navigate(['/login']);
                this.alertService.error('You are not logged in!', true);
            }
        });
    }

    constructor(private router: Router, private alertService: AlertService, private crawlService: CrawlService) {}
}
