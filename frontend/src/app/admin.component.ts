import { Component, OnInit } from '@angular/core';
import { User } from './user';
import { LoginService } from './login.service';

declare var jquery: any;
declare var $: any;


@Component({
    templateUrl: './admin.component.html',
    styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {
    ngOnInit(): void {
        console.log(this.login.loggedUser);
        $(document).ready(function() {
            $('.navbar-nav [data-toggle="tooltip"]').tooltip();
            $('.navbar-twitch-toggle').on('click', function(event) {
                event.preventDefault();
                $('.navbar-twitch').toggleClass('open');
            });
            $('.nav-style-toggle').on('click', function(event) {
                event.preventDefault();
                const $current = $('.nav-style-toggle.disabled');
                $(this).addClass('disabled');
                $current.removeClass('disabled');
                $('.navbar-twitch').removeClass('navbar-' + $current.data('type'));
                $('.navbar-twitch').addClass('navbar-' + $(this).data('type'));
            });
        });
    }

    onNavbarToggle(): void {
        event.preventDefault();
    }

    constructor(private login: LoginService) {}
}
