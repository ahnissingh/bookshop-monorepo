import { useState, useEffect, useRef } from 'react';

/**
 * @template T
 * @param {T} value
 * @param {number} delayMs
 * @param {(next: T) => void} [onDebouncedChange] Runs in the timer callback (not inside a React effect) when the committed debounced value changes.
 * @returns {T}
 */
export function useDebouncedValue(value, delayMs = 350, onDebouncedChange) {
    const [debounced, setDebounced] = useState(value);
    const onChangeRef = useRef(onDebouncedChange);

    useEffect(() => {
        onChangeRef.current = onDebouncedChange;
    }, [onDebouncedChange]);

    useEffect(() => {
        const t = setTimeout(() => {
            setDebounced((prev) => {
                if (prev !== value) {
                    onChangeRef.current?.(value);
                }
                return value;
            });
        }, delayMs);
        return () => clearTimeout(t);
    }, [value, delayMs]);

    return debounced;
}
