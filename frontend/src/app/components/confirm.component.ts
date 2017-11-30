import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AlertService } from '../services/alert.service';

@Component({
    template: ''
})
export class ConfirmComponent implements OnInit {

    constructor(private router: Router, private alertService: AlertService) {}

    ngOnInit() {
        this.alertService.success('Your account has been validated! You can now login!', true);
        this.router.navigate(['/login']);
    }
}