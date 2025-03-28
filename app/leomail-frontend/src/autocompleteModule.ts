import Quill from 'quill';

class AutocompleteModule {
    private editor: Quill;
    private suggestions: string[] = ['{firstname}', '{lastname}', '{email}'];
    private suggestionBox: HTMLDivElement | null = null;

    constructor(editor: Quill) {
        this.editor = editor;
        this.createSuggestionBox();
        this.attachEvents();
    }

    private attachEvents() {
        this.editor.on('text-change', () => {
            this.updateSuggestions();
        });

        document.addEventListener('click', (event) => {
            if (this.suggestionBox && !this.suggestionBox.contains(event.target as Node)) {
                this.hideSuggestions();
            }
        });
    }

    private createSuggestionBox() {
        this.suggestionBox = document.createElement('div');
        this.suggestionBox.className = 'autocomplete';
        document.body.appendChild(this.suggestionBox);
        this.suggestionBox.style.display = 'none';

        this.suggestionBox.addEventListener('mouseover', (event: MouseEvent) => {
            const target = event.target as HTMLElement;
            if (target && target.classList.contains('suggestion-item')) {
                target.style.backgroundColor = '#f0f0f0'; // Hintergrundfarbe beim Hover ändern
            }
        });

        this.suggestionBox.addEventListener('mouseout', (event: MouseEvent) => {
            const target = event.target as HTMLElement;
            if (target && target.classList.contains('suggestion-item')) {
                target.style.backgroundColor = ''; // Hintergrundfarbe zurücksetzen
            }
        });

        this.suggestionBox.addEventListener('click', (event: MouseEvent) => {
            const target = event.target as HTMLDivElement;
            if (target && target.dataset.value) {
                this.insertSuggestion(target.dataset.value);
                this.hideSuggestions();
            }
        });
    }

    private showSuggestions(triggerIndex: number, filterText: string) {
        if (this.suggestionBox) {
            // Filtere Vorschläge basierend auf dem eingegebenen Text
            const filteredSuggestions = this.suggestions.filter(suggestion =>
                suggestion.includes(`{${filterText}`)
            );

            // Zeige die gefilterten Vorschläge an oder alle, wenn keine übereinstimmen
            this.suggestionBox.innerHTML = (filteredSuggestions.length > 0 ? filteredSuggestions : this.suggestions)
                .map(suggestion =>
                    `<div data-value="${suggestion}" class="suggestion-item" style="font-size: 0.7rem; padding: 8px; cursor: pointer; border-bottom: 1px solid #ddd; color: #333;">${suggestion}</div>`
                ).join('');

            this.suggestionBox.style.display = 'block';

            // Positioniere die Vorschlagsbox
            const bounds = this.editor.getBounds(triggerIndex);
            if (bounds) {
                const offsetX = 250;
                this.suggestionBox.style.bottom = `${bounds.bottom}vh`;
                this.suggestionBox.style.left = `${bounds.left + offsetX}px`;
                this.suggestionBox.style.width = 'auto';
                this.suggestionBox.style.padding = '0.5%';
                this.suggestionBox.style.position = 'absolute';
                this.suggestionBox.style.border = '1px solid #ccc';
                this.suggestionBox.style.background = '#fff';
                this.suggestionBox.style.height = 'auto';
                this.suggestionBox.style.overflowY = 'auto';
            }
        }
    }

    private hideSuggestions() {
        if (this.suggestionBox) {
            this.suggestionBox.style.display = 'none';
        }
    }

    private focusEditor() {
        this.editor.focus();
    }


    private updateSuggestions() {

        const range = this.editor.getText().length;
        if (range) {
            // Holen Sie sich den gesamten Text bis zur Cursor-Position
            const currentText = this.editor.getText(0, range);

            // Überprüfen, ob `{` im Text vorhanden ist
            const triggerIndex = currentText.lastIndexOf('{');
            const closingBracketIndex = currentText.indexOf('}', range);

            if (closingBracketIndex !== -1 && closingBracketIndex < range) {
                this.hideSuggestions();
                return;
            }

            const textAfterTrigger = currentText.slice(triggerIndex + 1, range);
            if (triggerIndex !== -1 && !textAfterTrigger.includes('}')) {
                const textAfterTrigger = currentText.slice(triggerIndex + 1, range).trim();

                if (textAfterTrigger.length === 0 || textAfterTrigger === '}') {
                    this.showSuggestions(triggerIndex, '');
                } else {
                    this.showSuggestions(triggerIndex, textAfterTrigger);
                }
            } else {
                this.hideSuggestions();
            }
        } else {
            this.hideSuggestions();
        }
    }


    private insertSuggestion(value: string) {
        this.focusEditor(); // Sicherstellen, dass der Editor fokussiert ist

        const range = this.editor.getSelection();

        if (range) {
            const currentText = this.editor.getText();
            const start = currentText.lastIndexOf('{');

            this.editor.deleteText(start, range.index - start);
            this.editor.insertText(start, value);
            this.editor.setSelection(start + value.length, 0);
            this.hideSuggestions();
        }
    }
}

export default AutocompleteModule;
