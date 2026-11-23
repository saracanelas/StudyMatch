/** Joins class names, skipping empty values. */
export const cx = (...classes) => classes.filter(Boolean).join(" ");
