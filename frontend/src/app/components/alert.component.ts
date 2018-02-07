import { Component, OnInit } from '@angular/core';
import { AlertService } from '../services/alert.service';

@Component({
    selector: 'app-alert',
    templateUrl: '../views/alert.component.html'
})
export class AlertComponent implements OnInit {
    message: any;
    constructor(private alertService: AlertService) {}

    ngOnInit() {
        this.alertService.getMessage().subscribe(message => this.message = message);
    }
}
