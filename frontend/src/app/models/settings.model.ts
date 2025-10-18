export interface SettingsResponse {
  version: string;
  supportedFormats: string[];
  entityCounts: { [key: string]: number };
}

export interface UserPreferences {
  locale: string;
}
