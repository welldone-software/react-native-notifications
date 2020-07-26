export interface NotificationCategoryParams {
  identifier: string;
  actions: NotificationAction[];
}

export class NotificationCategory {
  identifier: string;
  actions: NotificationAction[];

  constructor(params: NotificationCategoryParams) {
    this.identifier = params.identifier;
    this.actions = params.actions;
  }
}

export interface NotificationTextInput {
  buttonTitle: string;
  placeholder: string;
}

export interface NotificationActionParams {
  identifier: string;
  activationMode: "foreground" | "authenticationRequired" | "destructive";
  title: string;
  authenticationRequired?: boolean;
  textInput?: NotificationTextInput;
  destructive?: boolean;
}

export class NotificationAction {
  identifier: string;
  activationMode: "foreground" | "authenticationRequired" | "destructive";
  title: string;
  authenticationRequired: boolean;
  textInput: NotificationTextInput | undefined;
  destructive: boolean;

  constructor(params: NotificationActionParams) {
    this.identifier = params.identifier;
    this.activationMode = params.activationMode;
    this.title = params.title;
    this.authenticationRequired = params.authenticationRequired || false;
    this.destructive = params.destructive || false;
    this.textInput = params.textInput;
  }
}
