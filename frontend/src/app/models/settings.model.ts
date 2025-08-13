export interface SettingsResponse {
  version: string;
  supportedFormats: string[];
  entityCounts: { [key: string]: number };
}