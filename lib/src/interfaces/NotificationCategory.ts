export interface NotificationCategoryParams {
  identifier: string;
  actions: NotificationAction[];
}

export class NotificationCategory {
  identifier: string
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
  activationMode: 'foreground' | 'authenticationRequired' | 'destructive';
  title: string;
  authenticationRequired: boolean;
  textInput?: NotificationTextInput;
}

export class NotificationAction {
  identifier: string;
  activationMode: 'foreground' | 'authenticationRequired' | 'destructive';
  title: string;
  authenticationRequired: boolean;
  textInput: NotificationTextInput | undefined;

  constructor(params: NotificationActionParams) {
    this.identifier = params.identifier;
    this.activationMode = params.activationMode;
    this.title = params.title;
    this.authenticationRequired = params.authenticationRequired;
    this.textInput = params.textInput;
  }
}