import { Injectable } from '@angular/core';
import { HttpParams, HttpClient, HttpResponse, HttpRequest, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Login } from '../data/login';
import { User } from '../data/user';
import { environment } from '../../environments/environment';
import { Quote } from '../data/quote';
import { headersToString } from 'selenium-webdriver/http';

@Injectable()
export class LoginService {
    constructor(private httpClient: HttpClient) {}

    login(login: Login): Observable<any> {
        return this.httpClient.post<User>(environment.apiBase + '/v1/users/login', login, {observe: 'response'});
    }

    register(user: User): Observable<User> {
        return this.httpClient.post<User>(environment.apiBase + '/v1/users/register', user, {
            params: new HttpParams().set('redirect', environment.redirectUrl + '/confirm')
        });
    }

    quote(): Observable<Quote> {
        return this.httpClient.get<Quote>(environment.apiBase + '/v1/server/quote');
    }
}
