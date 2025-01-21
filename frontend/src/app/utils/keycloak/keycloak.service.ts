import { Injectable } from '@angular/core';
import Keycloak from "keycloak-js";
import {environment} from "../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class KeycloakService {

  private _keycloak: Keycloak | undefined;

  constructor() { }

  get keycloak(): Keycloak {
    if(!this._keycloak) {
      this._keycloak = new Keycloak({
        url: environment.keycloakBaseUrl,
        realm: 'echohub',
        clientId: 'echohub-app'
      })
    }
    return this._keycloak;
  }

  async init() {
    await this.keycloak.init({
      onLoad: 'login-required'
    });
  }

  async login() {
    await this.keycloak.login();
  }

  get userId(): string {
    return this.keycloak.tokenParsed?.sub as string;
  }

  get isTokenValid(): boolean {
    return !this.keycloak.isTokenExpired();
  }

  get fullName(): string {
    return this.keycloak.tokenParsed?.['name'] as string;
  }

  logout() {
    return this.keycloak.logout({
      redirectUri: environment.angularBaseUrl
    });
  }

  accountManagement() {
    return this.keycloak.accountManagement();
  }
}
