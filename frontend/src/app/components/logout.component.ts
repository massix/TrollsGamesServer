import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AlertService } from '../services/alert.service';

@Component({
    template: ''
})
export class LogoutComponent {
    constructor(private router: Router, private alertService: AlertService) {
        localStorage.clear();
        router.navigate(['/login']);
        this.alertService.success('Logged out', true);
    }
}