import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { User } from '../data/user';
import { environment } from '../../environments/environment';

@Injectable()
export class UsersService {
    getAllUsers(): Observable<User[]> {
        return this.httpClient.get<User[]>(environment.apiBase + '/v1/users/get');
    }

    addUser(body: User): Observable<User> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.post<User>(environment.apiBase + '/v1/users/add', body, {headers: headers});
    }

    removeUser(body: User): Observable<User> {
        return this.httpClient.delete<User>(environment.apiBase + '/v1/users/remove/' + body.bggNick, {
            headers: new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'))
        });
    }

    modifyUser(user: User): Observable<User> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + localStorage.getItem('token'));
        return this.httpClient.patch<User>(environment.apiBase + '/v1/users/modify', user, {headers: headers});
    }

    constructor(private httpClient: HttpClient) {}
}
