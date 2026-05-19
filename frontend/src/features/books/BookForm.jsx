import { useForm } from 'react-hook-form';
import { useEffect, useState, useCallback } from 'react';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { mapFormValuesToBookRequest } from '../../api/bookApi';
import BookImageField from './BookImageField';

const GRADES = [
    'Nursery', 'Lower Kindergarten', 'Upper Kindergarten',
    'First', 'Second', 'Third', 'Fourth', 'Fifth', 'Sixth',
    'Seventh', 'Eighth', 'Ninth', 'Tenth', 'Eleventh', 'Twelfth',
];

const bookSchema = z.object({
    title: z.string().min(1, 'Title is required'),
    author: z.string().min(1, 'Author is required'),
    subtitle: z.string().optional().nullable(),
    price: z.preprocess(
        (a) => parseFloat(a),
        z.number({ invalid_type_error: 'Price must be a number' }).positive('Price must be greater than zero')
    ),
    quantity: z.preprocess(
        (a) => parseInt(a, 10),
        z.number({ invalid_type_error: 'Quantity must be a number' }).int('Quantity must be a whole number').min(0, 'Quantity cannot be negative')
    ),
    grade: z.string().min(1, 'Please select a valid grade'),
    description: z.string().optional().nullable(),
});

export default function BookForm({
    onSubmit,
    defaultValues,
    existingImageUrl,
    isSubmitting,
    submitLabel = 'Save',
}) {
    const [imageFile, setImageFile] = useState(null);
    const [isImageCompressing, setIsImageCompressing] = useState(false);

    const { register, handleSubmit, reset, formState: { errors } } = useForm({
        resolver: zodResolver(bookSchema),
        defaultValues: defaultValues || {
            title: '',
            author: '',
            subtitle: '',
            price: '',
            quantity: '',
            grade: '',
            description: '',
        },
    });

    useEffect(() => {
        if (defaultValues) reset(defaultValues);
    }, [defaultValues, reset]);

    const handleImageFileChange = useCallback((file) => {
        setImageFile(file);
    }, []);

    const handleFormSubmit = (values) => {
        const book = mapFormValuesToBookRequest(values);
        onSubmit({ book, file: imageFile ?? undefined });
    };

    const inputClass = 'w-full px-3 py-2 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-50 text-sm placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 dark:focus:border-indigo-400 transition-colors';
    const labelClass = 'block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5';
    const errorClass = 'text-xs text-red-500 dark:text-red-400 mt-1';

    const submitDisabled = isSubmitting || isImageCompressing;

    return (
        <form onSubmit={handleSubmit(handleFormSubmit)} className="lg:grid lg:grid-cols-[minmax(240px,280px)_1fr] lg:gap-8 lg:items-start">
            <div className="mb-6 lg:mb-0 lg:sticky lg:top-24">
                <BookImageField
                    existingImageUrl={existingImageUrl}
                    onFileChange={handleImageFileChange}
                    onCompressingChange={setIsImageCompressing}
                    disabled={submitDisabled}
                />
            </div>

            <div className="space-y-5 min-w-0">
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

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                    <div>
                        <label className={labelClass}>Price (₹)</label>
                        <input
                            type="number"
                            step="0.01"
                            min="0.01"
                            {...register('price')}
                            className={inputClass}
                            placeholder="0.00"
                        />
                        {errors.price && <p className={errorClass}>{errors.price.message}</p>}
                    </div>
                    <div>
                        <label className={labelClass}>Quantity</label>
                        <input
                            type="number"
                            step="1"
                            min="0"
                            {...register('quantity')}
                            className={inputClass}
                            placeholder="0"
                        />
                        {errors.quantity && <p className={errorClass}>{errors.quantity.message}</p>}
                    </div>
                    <div>
                        <label className={labelClass}>Grade</label>
                        <select
                            {...register('grade')}
                            className={inputClass}
                            defaultValue=""
                        >
                            <option value="" disabled>Select a grade</option>
                            {GRADES.map((g) => (
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
                        rows={4}
                        className={`${inputClass} resize-none`}
                        placeholder="Brief description of the book"
                    />
                </div>

                <div className="flex justify-end pt-2">
                    <button
                        type="submit"
                        disabled={submitDisabled}
                        className="px-5 py-2.5 rounded-lg bg-slate-900 dark:bg-slate-50 text-white dark:text-slate-900 text-sm font-medium hover:bg-slate-800 dark:hover:bg-slate-200 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                        {isImageCompressing ? 'Optimizing image…' : isSubmitting ? 'Saving…' : submitLabel}
                    </button>
                </div>
            </div>
        </form>
    );
}
