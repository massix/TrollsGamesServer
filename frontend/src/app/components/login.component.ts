import { Component, OnInit } from '@angular/core';
import { LoginService } from '../services/login.service';
import { Login } from '../data/login';
import { HttpResponse } from '@angular/common/http';
import { User } from '../data/user';
import { error } from 'util';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { AlertService } from '../services/alert.service';
import { Quote } from '../data/quote';

@Component({
    templateUrl: '../views/login.component.html',
    styleUrls: ['../styles/login.component.css']
})
export class LoginComponent implements OnInit {
    login: Login = new Login();
    error: string;
    quote: Quote;

    ngOnInit() {
        this.loginService.quote().subscribe(data => this.quote = data);
    }

    onSubmit(): void {
        this.error = null;
        console.log('on submit');
        this.loginService.login(this.login).subscribe((data: HttpResponse<User>) => {
            console.log('received response');
            if (data.headers.has('authorization')) {
                localStorage.setItem('token', data.headers.get('authorization').replace('Bearer ', ''));
                this.alertService.success('Logged in', true);
                console.log('routing to admin');
                this.router.navigateByUrl('/admin/(adminoutlet:users)');
            } else {
                this.alertService.error('Missing header?');
            }
        },

        (err: HttpErrorResponse) => {
            if (err.status === 404) {
                this.alertService.error('User not found');
            } else {
                this.alertService.error(err.error);
            }

            this.login.email = '';
            this.login.password = '';
        });
    }

    constructor(private loginService: LoginService, private router: Router, private alertService: AlertService) {}
}
