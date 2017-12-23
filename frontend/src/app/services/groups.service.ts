import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { Group, UsersGroups } from '../data/group';
import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';
import { User } from '../data/user';
import { headersToString } from 'selenium-webdriver/http';

@Injectable()
export class GroupsService {

  headers = new HttpHeaders().set('Authorization', `Bearer ${localStorage.getItem('token')}`);

  getAllGroups(): Observable<Group[]> {
    return this.httpClient.get<Group[]>(`${environment.apiBase}/v1/groups/get/all`, {headers: this.headers});
  }

  createGroup(group: Group): Observable<Group> {
    return this.httpClient.post<Group>(`${environment.apiBase}/v1/groups/create`, group, {headers: this.headers});
  }

  getMembers(group: Group): Observable<UsersGroups[]> {
    return this.httpClient.get<UsersGroups[]>(`${environment.apiBase}/v1/groups/${group.id}/members`, {headers: this.headers});
  }

  addMember(usersGroups: UsersGroups): Observable<UsersGroups> {
    return this.httpClient.post<UsersGroups>(
      `${environment.apiBase}/v1/groups/${usersGroups.groupId}/add`, usersGroups, {headers: this.headers}
    );
  }

  modifyGroup(group: Group): Observable<Group> {
    return this.httpClient.patch<Group>(`${environment.apiBase}/v1/groups/modify`, group, { headers: this.headers });
  }

  removeGroup(group: Group) {
    return this.httpClient.delete(`${environment.apiBase}/v1/groups/delete/${group.id}`, { headers: this.headers });
  }

  constructor(private httpClient: HttpClient) {}
}
