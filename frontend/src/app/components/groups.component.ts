import { Component, OnInit, style } from '@angular/core';
import { AlertService } from '../services/alert.service';
import { GroupsService } from '../services/groups.service';
import { Group, UsersGroups } from '../data/group';
import { User } from '../data/user';
import { UsersService } from '../services/users.service';

@Component({
  template:  `
  <div class="container">
    <div class="col-xs-12 col-sm-3">
      <div class="group-add">
        <form class="form-group" (ngSubmit)="createGroup()" name="addGroup">
          <input type="text" placeholder="name" [(ngModel)]="newGroup.name" name="groupName" />
          <input type="text" placeholder="description" [(ngModel)]="newGroup.description" name="groupDescription" />
          <select class="form-control" name="groupStatus" [(ngModel)]="newGroup.status">
            <option>PUBLIC</option>
            <option>INVITE_ONLY</option>
          </select>
          <div class="btn-group">
            <button class="btn btn-sm fa fa-plus" type="submit"></button>
          </div>
        </form>
      </div>
      <div class="group" [ngClass]="{'active': selectedGroup === group}" *ngFor="let group of groups" (click)="selectedGroup = group">
        <span class="fa fa-remove group-remove" (click)="removeGroup(group)"></span>
        <span class="group-name">({{ group.id }}) {{ group.name }}</span><br />
        <span class="group-description">{{ group.description }}</span><br />
        <span class="group-members-count" *ngIf="group.members">{{ group.members.length }} members<br /></span>
        <span class="group-status">{{ group.status }}</span>
      </div>
    </div>

    <div class="col-xs-12 col-sm-9" *ngIf="selectedGroup">
      <form (ngSubmit)="modifyGroup()">
        <input class="form-control" [(ngModel)]="selectedGroup.name" name="groupName" />
        <input class="form-control" [(ngModel)]="selectedGroup.description" name="groupDescription" />
        <select class="form-control" [(ngModel)]="selectedGroup.status" name="groupStatus">
          <option>PUBLIC</option>
          <option>INVITE_ONLY</option>
          <option>CLOSED</option>
        </select>
        <button type="btn btn-sm submit">Submit</button>
      </form>
      <div class="table" *ngIf="selectedGroup.members">
        <table class="table table-dark table-hover table-condensed">
            <thead class="thead-light">
                <tr>
                    <th scope="col">Name</th>
                    <th scope="col">Role</th>
                </tr>
            </thead>
            <tbody>
                <tr *ngFor="let member of selectedGroup.members">
                    <th scope="row">
                      <span class="fa fa-remove control" (click)="removeMember(member)"></span>
                      <span class="fa fa-save control" (click)="saveMember(member)"></span>&nbsp;{{member.userId}}
                    </th>
                    <td>
                      <select [(ngModel)]="member.role">
                        <option>ADMINISTRATOR</option>
                        <option>MEMBER</option>
                      </select>
                    </td>
                </tr>
                <tr>
                  <td>
                    <span class="fa fa-save control" (click)="addMember(newMember)"></span>&nbsp;
                    <input autocomplete [(ngModel)]="newMember.userId"
                                        [config]="{'placeholder': 'search for user', 'sourceField': ['bggNick'] }"
                                        [items]="usersSearch" />
                  </td>
                  <td>
                    <select [(ngModel)]="newMember.role">
                      <option>ADMINISTRATOR</option>
                      <option>MEMBER</option>
                    </select>
                  </td>
                </tr>
            </tbody>
        </table>
      </div>
    </div>
  </div>
  `,
  styles: [`
    .group {
      padding: 2px;
      border-bottom: 1px dotted black;
    }

    .group-name {
      font-size: 1.2em;
      font-weigth: 900;
    }

    .group-description {
      font-size: 0.8em;
      font-weight: 500;
    }

    .group-status {
      font-size: 0.7em;
      font-weight: 200;
    }

    .group:hover,
    .active {
      background-color: rgb(231, 230, 222);
      padding-left: 5em;
    }

    .control, .group-remove {
      cursor: pointer;
    }

    .group-name {

    }
  `]
})
export class GroupsComponent implements OnInit {
    groups: Group[];
    newGroup: Group = new Group();
    selectedGroup: Group;
    newMember: UsersGroups = new UsersGroups();
    usersSearch: User[] = [];

    ngOnInit(): void {
      this.getAllGroups();
      this.usersService.getAllUsers().subscribe(data => this.usersSearch = data);
      this.newMember.role = 'MEMBER';
    }

    getAllGroups(): void {
      this.groupsService.getAllGroups().subscribe(data => {
        this.groups = data;
        this.groups.forEach(group => {
          this.groupsService.getMembers(group).subscribe(members => group.members = members);
        });
      });
    }

    createGroup(): void {
      console.log(`Creating group ${this.newGroup.name} (${this.newGroup.description} | ${this.newGroup.status})`);
      this.groupsService.createGroup(this.newGroup).subscribe(
        data => {
          this.getAllGroups();
          this.newGroup = new Group();
        },
        error => this.alert.error(`Could not create group (${error})`)
      );
    }

    removeMember(member: UsersGroups) {
      console.log(`Removing member ${member.userId} from ${member.groupId}`);
    }

    saveMember(member: UsersGroups) {
      console.log(`Saving member ${member.userId} for ${member.groupId} with role ${member.role}`);
      this.groupsService.addMember(member).subscribe(data => {
        this.groupsService.getMembers(this.selectedGroup).subscribe(members => this.selectedGroup.members = members);
      });
    }

    addMember(member: UsersGroups) {
      this.newMember.groupId = this.selectedGroup.id;
      console.log(`adding ${this.newMember.userId} as ${this.newMember.role} @ ${this.newMember.groupId}`);
      this.groupsService.addMember(this.newMember).subscribe(data => {
        this.groupsService.getMembers(this.selectedGroup).subscribe(members => {
          this.selectedGroup.members = members;
        });

        this.newMember = new UsersGroups();
        this.newMember.role = 'MEMBER';
      });
    }

    searchUser(query: string) {
      console.log(`search for user ${query}`);
    }

    modifyGroup() {
      console.log(`set name ${this.selectedGroup.name} for ${this.selectedGroup.id}`);
      this.groupsService.modifyGroup(this.selectedGroup).subscribe(d => this.getAllGroups());
    }

    removeGroup(group: Group) {
      console.log(`removing group ${group.id}`);
      this.groupsService.removeGroup(group).subscribe(d => this.getAllGroups(), e => this.getAllGroups());
    }

    constructor(private alert: AlertService, private groupsService: GroupsService, private usersService: UsersService) {}
}
