import { useForm } from 'react-hook-form';
import { useEffect } from 'react';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';

const GRADES = [
    'Nursery', 'Lower Kindergarten', 'Upper Kindergarten',
    'First', 'Second', 'Third', 'Fourth', 'Fifth', 'Sixth',
    'Seventh', 'Eighth', 'Ninth', 'Tenth', 'Eleventh', 'Twelfth'
];

const bookSchema = z.object({
    title: z.string().min(1, 'Title is required'),
    author: z.string().min(1, 'Author is required'),
    subtitle: z.string().optional().nullable(),
    price: z.preprocess((a) => parseFloat(a), z.number({ invalid_type_error: 'Price must be a number' }).min(0, 'Price must be positive')),
    grade: z.string().min(1, 'Please select a valid grade'),
    description: z.string().optional().nullable(),
});

export default function BookForm({ onSubmit, defaultValues, isSubmitting, submitLabel = 'Save' }) {
    const { register, handleSubmit, reset, formState: { errors } } = useForm({
        resolver: zodResolver(bookSchema),
        defaultValues: defaultValues || {
            title: '',
            author: '',
            subtitle: '',
            price: '',
            grade: '',
            description: '',
        },
    });

    useEffect(() => {
        if (defaultValues) reset(defaultValues);
    }, [defaultValues, reset]);

    const inputClass = 'w-full px-3 py-2 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-50 text-sm placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 dark:focus:border-indigo-400 transition-colors';
    const labelClass = 'block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5';
    const errorClass = 'text-xs text-red-500 dark:text-red-400 mt-1';

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                    <label className={labelClass}>Title</label>
                    <input
                        {...register('title')}
                        className={inputClass}
                        placeholder="Enter book title"
                    />
                    {errors.title && <p className={errorClass}>{errors.title.message}</p>}
                </div>
                <div>
                    <label className={labelClass}>Author</label>
                    <input
                        {...register('author')}
                        className={inputClass}
                        placeholder="Enter author name"
                    />
                    {errors.author && <p className={errorClass}>{errors.author.message}</p>}
                </div>
            </div>

            <div>
                <label className={labelClass}>Subtitle</label>
                <input
                    {...register('subtitle')}
                    className={inputClass}
                    placeholder="Optional subtitle"
                />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                    <label className={labelClass}>Price (₹)</label>
                    <input
                        type="number"
                        step="0.01"
                        {...register('price')}
                        className={inputClass}
                        placeholder="0.00"
                    />
                    {errors.price && <p className={errorClass}>{errors.price.message}</p>}
                </div>
                <div>
                    <label className={labelClass}>Grade</label>
                    <select
                        {...register('grade')}
                        className={inputClass}
                        defaultValue=""
                    >
                        <option value="" disabled>Select a grade</option>
                        {GRADES.map(g => (
                            <option key={g} value={g}>{g}</option>
                        ))}
                    </select>
                    {errors.grade && <p className={errorClass}>{errors.grade.message}</p>}
                </div>
            </div>

            <div>
                <label className={labelClass}>Description</label>
                <textarea
                    {...register('description')}
                    rows={3}
                    className={`${inputClass} resize-none`}
                    placeholder="Brief description of the book"
                />
            </div>

            <div className="flex justify-end pt-2">
                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="px-5 py-2 rounded-lg bg-slate-900 dark:bg-slate-50 text-white dark:text-slate-900 text-sm font-medium hover:bg-slate-800 dark:hover:bg-slate-200 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                    {isSubmitting ? 'Saving...' : submitLabel}
                </button>
            </div>
        </form>
    );
}
