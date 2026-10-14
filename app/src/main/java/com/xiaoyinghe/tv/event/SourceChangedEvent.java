package com.xiaoyinghe.tv.event;

public final class SourceChangedEvent {
    public final boolean selectionChanged;
    public SourceChangedEvent() { this(false); }
    public SourceChangedEvent(boolean selectionChanged) { this.selectionChanged = selectionChanged; }
}
