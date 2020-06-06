package robin.freezetag.main;

public enum Role {
	HUNTER, FIREMAN, RUNNER;
	
	public static String getPrefix(Role role) {
		if(role == HUNTER) {
			return "§b";
		}
		if(role == FIREMAN) {
			return "§6";
		}
		return "§7";
	}
	public static String getGroupName(Role role) {
		if(role == HUNTER) {
			return FileManager.getMessage("groups.hunter");
		}
		if(role == FIREMAN) {
			return FileManager.getMessage("groups.fireman");
		}
		return FileManager.getMessage("groups.runner");
	}
}
