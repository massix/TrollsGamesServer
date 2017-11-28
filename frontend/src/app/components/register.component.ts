import { Component, OnInit } from '@angular/core';
import { LoginService } from '../services/login.service';
import { Quote } from '../data/quote';
import { User } from '../data/user';
import { AlertService } from '../services/alert.service';
import { Router } from '@angular/router';

@Component({
    templateUrl: '../views/register.component.html',
    styleUrls: ['../styles/login.component.css']
})
export class RegisterComponent implements OnInit {
    quote: Quote;
    user = new User();
    confirmPassword: string;

    ngOnInit () {
        this.loginService.quote().subscribe(data => this.quote = data);
    }

    onSubmit() {
        if (! this.user.bggHandled) {
            this.user.bggNick = this.user.forumNick;
        }

        this.loginService.register(this.user).subscribe(
            data => {
                this.alertService.success('Done! Please verify your email for a confirmation link!', true);
                this.router.navigate(['/login']);
            },
            error => this.alertService.error('Something wrong happened. Please refresh the page and retry!')
        );
    }

    constructor(private loginService: LoginService, 
        private alertService: AlertService,
        private router: Router) {}
}