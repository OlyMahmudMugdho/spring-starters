import { useState } from "react";
import { useQuery, useMutation, gql } from "@apollo/client";

const GET_BOOKS = gql`
    query {
        findAllBooks {
            id
            title
            author
            price
        }
    }
`;

const CREATE_BOOK = gql`
    mutation CreateBook($bookInput: BookInput!) {
        createBook(bookInput: $bookInput) {
            id
            title
        }
    }
`;

const UPDATE_BOOK = gql`
    mutation UpdateBook($id: ID!, $bookInput: BookInput!) {
        updateBook(id: $id, bookInput: $bookInput) {
            id
            title
        }
    }
`;

const DELETE_BOOK = gql`
    mutation DeleteBook($id: ID!) {
        deleteBook(id: $id)
    }
`;

interface Book {
    id: string;
    title: string;
    author: string;
    price: number;
}

interface BookInput {
    title: string;
    author: string;
    price: number;
}

export default function App() {
    const { loading, error, data, refetch } = useQuery<{ findAllBooks: Book[] }>(GET_BOOKS);
    const [createBook] = useMutation(CREATE_BOOK, { onCompleted: () => refetch() });
    const [updateBook] = useMutation(UPDATE_BOOK, { onCompleted: () => refetch() });
    const [deleteBook] = useMutation(DELETE_BOOK, { onCompleted: () => refetch() });

    const [formData, setFormData] = useState<BookInput>({ title: "", author: "", price: 0 });
    const [editId, setEditId] = useState<string | null>(null);

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (editId) {
            updateBook({ variables: { id: editId, bookInput: formData } });
        } else {
            createBook({ variables: { bookInput: formData } });
        }
        setFormData({ title: "", author: "", price: 0 });
        setEditId(null);
    };

    const handleEdit = (book: Book) => {
        setFormData({ title: book.title, author: book.author, price: book.price });
        setEditId(book.id);
    };

    const handleDelete = (id: string) => {
        deleteBook({ variables: { id } });
    };

    if (loading) return <p>Loading...</p>;
    if (error) return <p>Error: {error.message}</p>;

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-3xl font-bold mb-4">Book Store</h1>
            <form onSubmit={handleSubmit} className="mb-8">
                <div className="grid grid-cols-1 gap-4">
                    <input
                        type="text"
                        placeholder="Title"
                        value={formData.title}
                        onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                        className="input input-bordered w-full"
                        required
                    />
                    <input
                        type="text"
                        placeholder="Author"
                        value={formData.author}
                        onChange={(e) => setFormData({ ...formData, author: e.target.value })}
                        className="input input-bordered w-full"
                        required
                    />
                    <input
                        type="number"
                        placeholder="Price"
                        value={formData.price}
                        onChange={(e) => setFormData({ ...formData, price: parseFloat(e.target.value) || 0 })}
                        className="input input-bordered w-full"
                        required
                    />
                    <button type="submit" className="btn btn-primary">
                        {editId ? "Update Book" : "Add Book"}
                    </button>
                </div>
            </form>

            <div className="overflow-x-auto">
                <table className="table w-full">
                    <thead>
                    <tr>
                        <th>Title</th>
                        <th>Author</th>
                        <th>Price</th>
                        <th>Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    {data?.findAllBooks.map((book) => (
                        <tr key={book.id}>
                            <td>{book.title}</td>
                            <td>{book.author}</td>
                            <td>${book.price.toFixed(2)}</td>
                            <td>
                                <button onClick={() => handleEdit(book)} className="btn btn-sm btn-info mr-2">
                                    Edit
                                </button>
                                <button onClick={() => handleDelete(book.id)} className="btn btn-sm btn-error">
                                    Delete
                                </button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}


